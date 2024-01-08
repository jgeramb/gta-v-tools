package dev.justix.gtavtools.tools.mission;

import dev.justix.gtavtools.config.ApplicationConfig;
import dev.justix.gtavtools.logging.Level;
import dev.justix.gtavtools.logging.Logger;
import dev.justix.gtavtools.tools.Category;
import dev.justix.gtavtools.tools.Tool;
import dev.justix.gtavtools.util.DNSUtil;
import dev.justix.gtavtools.util.InterfaceNavigationUtil;
import org.pcap4j.core.BpfProgram;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.IpV4Packet;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.justix.gtavtools.util.SystemUtil.*;

public class ReplayGlitch extends Tool {

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

    private boolean cancel;

    public ReplayGlitch(Logger logger) {
        super(logger, Category.MISSION, "Replay Glitch");
    }

    @Override
    public void execute() {
        this.cancel = false;

        // Determine telemetry servers
        final List<String> cnameRecords = DNSUtil.getCNAMERecords("prod.telemetry.ros.rockstargames.com");

        if(cnameRecords.isEmpty()) {
            this.logger.log(Level.SEVERE, "CNAME record not found");
            return;
        }

        final List<String> telemetryServers = DNSUtil.getARecords(cnameRecords.get(0));

        // Watch network traffic
        try {
            final String adapterName = (String) ApplicationConfig.CONFIG.get("networkAdapterName");
            final PcapNetworkInterface device = Pcaps.findAllDevs()
                    .stream()
                    .filter(networkInterface -> networkInterface.getDescription().equals(adapterName))
                    .findFirst()
                    .orElse(null);

            if(device == null) {
                this.logger.log(Level.SEVERE, "Network interface not found");
                return;
            }

            logger.log(Level.INFO, "Waiting for telemetry request...");

            final AtomicBoolean networkDisabled = new AtomicBoolean(false);
            final Object networkLock = new Object();

            boolean connectionOpened = false;

            AtomicBoolean packetSent = new AtomicBoolean(false),
                    packedReceived = new AtomicBoolean(false);

            while(!packedReceived.get() && !this.cancel) {
                try (final PcapHandle handle = device.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10)) {
                    handle.setFilter("tcp", BpfProgram.BpfCompileMode.OPTIMIZE);

                    Packet packet;

                    while((packet = handle.getNextPacketEx()) != null) {
                        if(!packet.contains(TcpPacket.class)) continue;
                        if(!packet.contains(IpV4Packet.class)) continue;

                        final TcpPacket tcpPacket = packet.get(TcpPacket.class);
                        final int srcPort = tcpPacket.getHeader().getSrcPort().valueAsInt(),
                                dstPort = tcpPacket.getHeader().getDstPort().valueAsInt();

                        if(dstPort == 80) {
                            if(packetSent.get()) continue;

                            if(tcpPacket.getPayload() == null) {
                                if(!networkDisabled.get() && tcpPacket.getHeader().getSyn() && !tcpPacket.getHeader().getAck()) {
                                    final IpV4Packet ipV4Packet = packet.get(IpV4Packet.class);
                                    final InetAddress destAddress = ipV4Packet.getHeader().getDstAddr();

                                    // Check if destination address is a telemetry server
                                    if(!telemetryServers.contains(destAddress.getHostAddress()))
                                        continue;

                                    connectionOpened = true;

                                    if(DEBUG)
                                        continue;

                                    new Thread(() -> {
                                        Thread.currentThread().setName("HTTP Packet Thread");

                                        if(networkDisabled.get())
                                            return;

                                        networkDisabled.set(true);

                                        sleep(275L);

                                        setNetworkAdapterEnabled(false);

                                        synchronized (networkLock) {
                                            networkLock.notify();
                                        }

                                        sleep(16750L);
                                        keyPress("ENTER", 50L);

                                        // Reconnect network
                                        sleep(5000L);

                                        setNetworkAdapterEnabled(true);

                                        sleep(15000L);

                                        // Open Social Club
                                        keyPress("HOME", 50L);
                                        sleep(1250L);

                                        // Reconnect and close pop-up
                                        robot().mouseMove(1210, 334);
                                        sleep(150L);
                                        mouseClick("LEFT", 100L);

                                        sleep(4000L);

                                        // Close pop-up
                                        keyPress("ESCAPE", 50L);
                                        sleep(3500L);

                                        InterfaceNavigationUtil.openPlayOnlineOptions(true);

                                        // Select 'Invite-only session'
                                        keyPress("DOWN", 50L);
                                        sleep(150L);
                                        keyPress("ENTER", 50L);
                                        sleep(750L);

                                        // Accept warning
                                        keyPress("ENTER", 50L);

                                        logger.log(Level.INFO, "Glitch completed");
                                    }).start();
                                }

                                continue;
                            }

                            if(!connectionOpened)
                                continue;

                            final String[] lines = new String(tcpPacket.getPayload().getRawData()).split("\r\n");

                            if (lines.length > 0) {
                                final String[] requestLine = lines[0].split(" ");

                                if (requestLine.length < 2)
                                    continue;

                                String path = requestLine[1];

                                if (!path.endsWith("/gameservices/Telemetry.asmx/SubmitCompressed")) {
                                    if(!DEBUG) {
                                        new Thread(() -> {
                                            if (packetSent.get()) return;

                                            synchronized (networkLock) {
                                                try {
                                                    networkLock.wait();
                                                } catch (InterruptedException ignore) {
                                                }
                                            }

                                            setNetworkAdapterEnabled(true);

                                            networkDisabled.set(false);
                                        }).start();
                                    }

                                    continue;
                                }

                                logger.log(Level.INFO, "Telemetry request sent at " + TIME_FORMAT.format(new Date()));

                                packetSent.set(true);
                            }
                        } else if(srcPort == 80) {
                            if(tcpPacket.getPayload() == null) continue;
                            if(!packetSent.get()) continue;

                            packedReceived.set(true);

                            logger.log(Level.INFO, "Telemetry response received at " + TIME_FORMAT.format(new Date()));

                            break;
                        }
                    }
                } catch (TimeoutException ignore) {
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An error occurred while executing tool: " + ex.getMessage());
        }
    }

    @Override
    public void forceStop() {
        this.cancel = true;
    }

    private static void setNetworkAdapterEnabled(boolean enabled) {
        try {
            Runtime.getRuntime().exec(new String[] {
                    "netsh",
                    "interface",
                    "set",
                    "interface",
                    String.format("\"%s\"", ApplicationConfig.CONFIG.get("networkInterfaceName")),
                    enabled ? "enable" : "disable"
            }).waitFor();
        } catch (IOException | InterruptedException ignore) {
        }
    }

}
