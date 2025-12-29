//
//  serverTimeApp.swift
//  serverTime
//
//  Created by suatecmac03 on 12/8/25.
//

import SwiftUI

@main
struct serverTimeApp: App {
    
    let store = Store(initialState: ClockFeature.State()) {
            ClockFeature()
        }
    
    var body: some Scene {
        WindowGroup {
            //ContentView()
            ContentView02(store: store)
        }
    }
}
