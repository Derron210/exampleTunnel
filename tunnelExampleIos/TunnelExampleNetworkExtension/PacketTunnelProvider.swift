//
//  PacketTunnelProvider.swift
//  TunnelExampleNetworkExtension
//

import NetworkExtension

enum TunnelError: Error {
    case serverAddressNotSet
}

class PacketTunnelProvider: NEPacketTunnelProvider {
    
    private var session: NWUDPSession!
    private var settings: NEPacketTunnelNetworkSettings!
    
    private func getTunnelNetworkSettings() async throws -> NEPacketTunnelNetworkSettings {
        guard let serverAddress = protocolConfiguration.serverAddress else {
            throw TunnelError.serverAddressNotSet
        }
        let settings = NEPacketTunnelNetworkSettings(tunnelRemoteAddress: serverAddress)
        
        settings.ipv4Settings = NEIPv4Settings(addresses: [ "10.0.100.5" ],
                                               subnetMasks: [ "255.255.255.255" ])
        settings.ipv4Settings!.includedRoutes = [NEIPv4Route(destinationAddress: "0.0.0.0", subnetMask: "128.0.0.0"),
                                                 NEIPv4Route(destinationAddress: "128.0.0.0", subnetMask: "128.0.0.0")
        ]
        
        settings.ipv4Settings!.excludedRoutes = [NEIPv4Route(destinationAddress: serverAddress, subnetMask: "255.255.255.255")]
        return settings
    }
    
    override func startTunnel(options: [String : NSObject]? = nil) async throws {
        NSLog("Start Tunnel")

        self.settings = try await self.getTunnelNetworkSettings()
        try await self.startSession()
        try await self.setTunnelNetworkSettings(self.settings)
        
        self.session.setReadHandler(self.handleRead, maxDatagrams: 1000)
        self.startReceivingInnerData()
    }
    
    private func startSession() async throws {
        NSLog("Starting session")
        let endpoint = NWHostEndpoint(hostname: settings.tunnelRemoteAddress, port: "1111")
        session = createUDPSession(to: endpoint, from: nil)
        try await session.write(data: "Hello server\r\n".data(using: .ascii)!)
    }
    
    private func handleRead(_ datagrams: [Data]?, _ error: (any Error)?) {
        NSLog("Handle read")
        if error != nil || datagrams == nil {
            NSLog("Error startReceivingExternalData \(error)")
            return
        }
        let datagrams = datagrams!
        for data in datagrams {
            
            NSLog("SOMEDATA \(data.count)")
            if data.count > 0 {
                 
                let nePacket = NEPacket(data: data, protocolFamily: sa_family_t(AF_INET))
                let result = self.packetFlow.writePacketObjects([nePacket])
                NSLog("data result: \(result)")
            
                
            }
        }
    }
    
    private func startReceivingInnerData() {
        self.packetFlow.readPacketObjects { packets in
            Task {
                NSLog("Received some packets, count=\(packets.count)")
                for packet in packets {
                    try! await self.session.write(data: packet.data)
                }
                self.startReceivingInnerData()
            }
        }
    }
    
    override func stopTunnel(with reason: NEProviderStopReason, completionHandler: @escaping () -> Void) {
        super.stopTunnel(with: reason, completionHandler: completionHandler)
        NSLog("stopTunnel was called")
        completionHandler()
    }
    
    override func handleAppMessage(_ messageData: Data, completionHandler: ((Data?) -> Void)?) {
        if let handler = completionHandler {
            handler(messageData)
        }
    }
    
    override func sleep(completionHandler: @escaping () -> Void) {
        // Add code here to get ready to sleep.
        completionHandler()
    }
    
    override func wake() {
        // Add code here to wake up.
    }
}


extension NWUDPSession {
    
    func write(data: Data) async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            self.writeDatagram(data) { error in
                if let error = error {
                    return continuation.resume(throwing: error)
                }
                continuation.resume()
            }
        }
    }
}

extension Data {
    var bytes: [UInt8] {
        return [UInt8](self)
    }
    
    func copy(start: Int, count: Int) -> Data {
        let arr = self.bytes
        var res = Data(capacity: count)
        res.append(contentsOf: arr[start..<start + count])
        return res
    }
}
