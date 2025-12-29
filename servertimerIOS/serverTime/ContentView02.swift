//
//  ContentView02.swift
//  serverTime
//
//  Created by suatecmac03 on 12/12/25.
//
import SwiftUI
import ComposableArchitecture

struct ContentView02: View {
    let store: StoreOf<ClockFeature>

    var body: some View {
        WithPerceptionTracking {
            GeometryReader { geo in
                let isLandscape = geo.size.width > geo.size.height

                ZStack {
                    backgroundGradient(isLandscape: isLandscape)
                        .ignoresSafeArea()

                    if isLandscape {
                        HStack(spacing: 0) {
                            TimePanelView(
                                title: "UTC",
                                dateText: store.utcDateText,
                                timeText: store.utcTimeText,
                                accent: .neonCyan,
                                isLandscape: isLandscape
                            )

                            DividerNeonVertical()

                            TimePanelView(
                                title: "KST (Asia/Seoul)",
                                dateText: store.kstDateText,
                                timeText: store.kstTimeText,
                                accent: .neonPink,
                                isLandscape: isLandscape
                            )
                        }
                    } else {
                        VStack(spacing: 0) {
                            TimePanelView(
                                title: "UTC",
                                dateText: store.utcDateText,
                                timeText: store.utcTimeText,
                                accent: .neonCyan,
                                isLandscape: isLandscape
                            )

                            DividerNeonHorizontal()

                            TimePanelView(
                                title: "KST (Asia/Seoul)",
                                dateText: store.kstDateText,
                                timeText: store.kstTimeText,
                                accent: .neonPink,
                                isLandscape: isLandscape
                            )
                        }
                    }
                }
            }
            // 여기서 타이머 시작/정지 액션 날림
            .onAppear {
                UIApplication.shared.isIdleTimerDisabled = true
                store.send(.onAppear)
            }
            .onDisappear {
                UIApplication.shared.isIdleTimerDisabled = false
                store.send(.onDisappear)
            }
        }
    }

    // 기존에 쓰던 backgroundGradient 함수 있으면 그대로 두고,
    // 없으면 아래처럼 추가
    @ViewBuilder
    private func backgroundGradient(isLandscape: Bool) -> some View {
        if isLandscape {
            LinearGradient(
                colors: [
                    .neonCyan.opacity(0.7),
                    .neonBackground,
                    .neonPink.opacity(0.7)
                ],
                startPoint: .leading,
                endPoint: .trailing
            )
        } else {
            LinearGradient(
                colors: [
                    .neonCyan.opacity(0.7),
                    .neonBackground,
                    .neonPink.opacity(0.7)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }
    }
}
