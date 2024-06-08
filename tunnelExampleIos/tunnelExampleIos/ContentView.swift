//
//  ContentView.swift
//  TunnelExampleIos
//

import SwiftUI
import NetworkExtension

struct ContentView: View {
    @StateObject var viewModel = ViewModel()
    
    var body: some View {
        VStack(alignment: .center) {
            Image(systemName: "globe")
                .imageScale(.large)
                .foregroundStyle(.tint)
            Text("Tunnel example ios")
            
            HStack(alignment: .center) {
                Text("IP address:")
                
                TextField("Ip address", text: $viewModel.ipAddress)
                    .frame(width: 150)
                    .overlay(VStack{Divider().offset(x: 0, y: 15)})
            }
            
            Button("Install profile") {
                Task {
                    try? await viewModel.installProfile()
                }
            }.padding(.vertical, 32)
            
            Button("Open connection") {
                viewModel.connect()
            }
            .disabled(!viewModel.isInstalled)
    
        }
        .padding()
        .onAppear(perform: {
            viewModel.checkIfProfileInstalled()
        })
    }
}

#Preview {
    ContentView()
}

class ViewModel: ObservableObject {
    @Published public private(set) var isInstalled: Bool = false
    @Published var ipAddress: String = "255.255.255.255"
    
    private var manager: NETunnelProviderManager? = nil
    
    func connect() {
        guard let manager = manager else { return }
        do {
            try manager.connection.startVPNTunnel()
        } catch {
            debugPrint(error)
        }
    }
    
    func installProfile() async throws {
        
        let managers = try await NETunnelProviderManager.loadAllFromPreferences()
        
        let manager = managers.first ?? NETunnelProviderManager()
        
        manager.localizedDescription = "TunnelExample"

        let proto = NETunnelProviderProtocol()

        proto.providerBundleIdentifier = "com.example.TunnelExampleIos.TunnelExampleNetworkExtension"
        proto.serverAddress = ipAddress


        manager.protocolConfiguration = proto
        manager.isEnabled = true
        
        
        
        manager.saveToPreferences { e in
            if let e = e {
                print(e)
                return
            }
            self.manager = manager
            self.isInstalled = true
        }
    }

    func checkIfProfileInstalled() {
        NETunnelProviderManager.loadAllFromPreferences { managers, err in
            guard let manager = managers?.first, err == nil else {
                print(err)
                return
            }
            self.isInstalled = true
            self.manager = manager
            self.ipAddress = manager.protocolConfiguration?.serverAddress ?? ""
            
        }
    }
}
