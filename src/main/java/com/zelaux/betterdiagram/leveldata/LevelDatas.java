package com.zelaux.betterdiagram.leveldata;

import com.google.common.net.InetAddresses;
import com.zelaux.betterdiagram.annotations.DebugOnly;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LevelDatas {
    public static final String LOCAL_HOST = "127.0.0.1";
    private static LevelData currentData;
    private static final Path SERVER_LOCAL_DATA_PATH, LEVEL_DATA_PATH;
    private static final Pattern IPV4 = Pattern.compile("^(\\d{1,3}(?:\\.\\d{1,3}){3})(?::(?<port>\\d{1,5}))?$");
    private static final Pattern IPV6 = Pattern.compile("^\\[?([a-fA-F0-9:]+)\\]?(?::(?<port>\\d{1,5}))?$");
    private static final Pattern HOST = Pattern.compile("^([a-zA-Z0-9.-]+)(?::(?<port>\\d{1,5}))?$");
    private static final Pattern[] patterns = {IPV4, IPV6, HOST};
    private static final Pattern localHost = Pattern.compile(
        "127\\.0\\.0\\.1|localhost|((?:0{1,4}:){7}0{0,3}1|::(?:0{0,3}:){0,6}0{0,3}1|::1)"
    );

    static {

        Minecraft instance = Minecraft.getInstance();
        Path gameDir = instance.gameDirectory.toPath();
        SERVER_LOCAL_DATA_PATH = gameDir.resolve(".bcdiagram");
        LEVEL_DATA_PATH = SERVER_LOCAL_DATA_PATH.resolve("leveldata");
    }

    public static void disconnect() {
        if(currentData != null) currentData.disconnect();
        currentData = null;
    }

    public static void locateLevelData(ServerData serverData) {
        IntegratedServer singleplayerServer = Minecraft.getInstance().getSingleplayerServer();
        disconnect();
        if(singleplayerServer != null) {
            currentData = new SinglePlayerData(singleplayerServer.getWorldPath(LevelResource.ROOT).getParent());
            return;
            //DEBUG PURPOSE
            //serverData = new ServerData("", "localhost:3030", ServerData.Type.OTHER);
        }
        String locator = determineLocator(serverData);
        if(locator == null) return;
        LevelDatas.currentData = new LevelData(LevelDatas.LEVEL_DATA_PATH.resolve(escape(locator)));
    }

    private static String determineLocator(ServerData serverData) {
        if(serverData == null) return null;
        String rawIp = serverData.ip;
        Matcher matcher = null;
        for(Pattern pattern : patterns) {
            matcher = pattern.matcher(rawIp);
            if(!matcher.matches()) {
                matcher = null;
            } else break;
        }
        if(matcher == null) return null;
        String ipOrHost = matcher.group(1);
        String port = matcher.group("port");
        if(localHost.matcher(ipOrHost).matches()) {
            return "localhost:" + port;
        }

        if(!InetAddresses.isInetAddress(ipOrHost)) return ipOrHost;
        InetAddress address = InetAddresses.forString(ipOrHost);
        var inet6Address = convertToIpv6(address);
        if(address.isLoopbackAddress()) return "localhost:" + port;

        if(address.isSiteLocalAddress() || address.isLinkLocalAddress())
            return "%local%:" + inet6Address.getHostAddress() + ":" + port;

        return inet6Address.getHostAddress();
    }

    private static String escape(String locator) {
        if(locator == null || locator.isEmpty()) return "%u%";

        //ipv6 fix
        String result = locator.replace(":", "_");

        //ipv6 fix
        result = result.replace("[", "").replace("]", "");

        //windows thing
        result = result.replaceAll("[\\\\/*?\"<>|]", "_");

        // Windows ignoring dots... MS f..
        int dots = 0;
        for(int length = result.length() - 1; length >= 0; length--) {
            if(result.charAt(length) == '.') dots++;
            else break;
        }
        if(dots != 0) {
            result = result.substring(0, result.length() - dots) + "%".repeat(dots);
        }

        // longs names not good
        if(result.length() > 128) {
            result = result.substring(0, 128);
        }

        //windows reservers (-_-")
        if(isReservedWindowsName(result)) result = "_" + result;

        return result.isEmpty() ? "default" : result;
    }

    private static boolean isReservedWindowsName(String name) {
        String[] reserved = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4",
            "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2",
            "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        for(String s : reserved) {
            if(name.equalsIgnoreCase(s)) return true;
        }
        return false;
    }

    @SneakyThrows
    public static InetAddress convertToIpv6(InetAddress input) {
        if(input instanceof Inet4Address) {
            byte[] ipv4Bytes = input.getAddress();
            byte[] ipv6Bytes = new byte[16];

            ipv6Bytes[10] = (byte) 0xFF;
            ipv6Bytes[11] = (byte) 0xFF;

            System.arraycopy(ipv4Bytes, 0, ipv6Bytes, 12, 4);

            return (InetAddress.getByAddress(ipv6Bytes));
        } else if(input instanceof Inet6Address x) {
            return x;
        }
        throw new IllegalArgumentException("Unsupported address type: " + input.getClass());
    }

    @Nullable
    public static LevelData levelData() {
        return currentData;
    }
}
