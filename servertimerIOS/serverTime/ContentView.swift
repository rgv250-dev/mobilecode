//
//  ContentView.swift
//  serverTime
//
//  Created by suatecmac03 on 12/8/25.
//

import SwiftUI

extension Color {
    static let neonBackground = Color(red: 5/255, green: 8/255, blue: 22/255)     // #050816
    static let neonPanel      = Color(red: 7/255, green: 11/255, blue: 25/255)    // #070b19
    static let neonCyan       = Color(red: 0/255, green: 245/255, blue: 255/255)  // #00F5FF
    static let neonPink       = Color(red: 255/255, green: 78/255, blue: 205/255) // #FF4ECD
    static let neonText       = Color(red: 234/255, green: 255/255, blue: 253/255)// #EAFFFD
}

struct ContentView: View {
    
        
    @StateObject private var viewModel = TimeViewModel()
    
    var body: some View {
        GeometryReader { geo in
            let isLandscape = geo.size.width > geo.size.height
            
            ZStack {
                // 배경 그라디언트 (가로/세로에 따라 방향 변경)
                backgroundGradient(isLandscape: isLandscape)
                    .ignoresSafeArea()
                
                if isLandscape {
                    // 가로: 좌우 반반
                    HStack(spacing: 0) {
                        TimePanelView(
                            title: "UTC",
                            dateText: viewModel.utcDateText,
                            timeText: viewModel.utcTimeText,
                            accent: .neonCyan,
                            isLandscape: isLandscape
                        )
                        
                        DividerNeonVertical()
                        
                        TimePanelView(
                            title: "KST (Asia/Seoul)",
                            dateText: viewModel.kstDateText,
                            timeText: viewModel.kstTimeText,
                            accent: .neonPink,
                            isLandscape: isLandscape
                        )
                    }
                } else {
                    // 세로: 위아래 반반
                    VStack(spacing: 0) {
                        TimePanelView(
                            title: "UTC",
                            dateText: viewModel.utcDateText,
                            timeText: viewModel.utcTimeText,
                            accent: .neonCyan,
                            isLandscape: isLandscape
                        )
                        
                        DividerNeonHorizontal()
                        
                        TimePanelView(
                            title: "KST (Asia/Seoul)",
                            dateText: viewModel.kstDateText,
                            timeText: viewModel.kstTimeText,
                            accent: .neonPink,
                            isLandscape: isLandscape
                        )
                    }
                }
            }
            .onAppear {
                // 화면 잠금 방지 (가능한 한)
                UIApplication.shared.isIdleTimerDisabled = true
            }
            .onDisappear {
                // 필요하면 다시 허용
                UIApplication.shared.isIdleTimerDisabled = false
            }
        }
    }
    
    // 배경 그라디언트
    @ViewBuilder
    private func backgroundGradient(isLandscape: Bool) -> some View {
        if isLandscape {
            LinearGradient(
                colors: [
                    Color.neonCyan.opacity(0.7),
                    Color.neonBackground,
                    Color.neonPink.opacity(0.7)
                ],
                startPoint: .leading,
                endPoint: .trailing
            )
        } else {
            LinearGradient(
                colors: [
                    Color.neonCyan.opacity(0.7),
                    Color.neonBackground,
                    Color.neonPink.opacity(0.7)
                ],
                startPoint: .top,
                endPoint: .bottom
            )
        }
    }
}

struct TimePanelView: View {
    
    let title: String
    let dateText: String
    let timeText: String
    let accent: Color
    let isLandscape: Bool
    
    var body: some View {
        ZStack {
            // 패널 뒤쪽 네온 글로우
            RadialGradient(
                colors: [
                    accent.opacity(0.7),
                    .clear
                ],
                center: .center,
                startRadius: 10,
                endRadius: 400
            )
            .blur(radius: 20)
            
            // 실제 패널
            VStack(spacing: 0) {
                Spacer(minLength: 0)
                
                VStack(spacing: 22) {
                    // 타이틀
                    Text(title)
                        .font(.system(size: 24, weight: .black, design: .default))
                        .foregroundColor(accent)
                        .shadow(color: accent.opacity(0.85),
                                radius: 10, x: 0, y: 0)
                        .tracking(3)
                        .multilineTextAlignment(.center)
                    
                    // 얇은 네온 라인
                    Rectangle()
                        .fill(accent.opacity(0.5))
                        .frame(width: 120, height: 2)
                    
                    // 날짜 배지
                    Text(dateText)
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(Color.neonText.opacity(0.9))
                        .padding(.horizontal, 18)
                        .padding(.vertical, 8)
                        .background(
                            Color.white.opacity(0.03)
                                .clipShape(Capsule())
                        )
                        .overlay(
                            Capsule()
                                .stroke(Color.white.opacity(0.4), lineWidth: 1)
                        )
                    
                    // 시간 네온 텍스트
                    NeonTimeText(
                        text: timeText,
                        accent: accent,
                        isLandscape: isLandscape
                    )
                }
                .padding(.horizontal, isLandscape ? 32 : 24)
                .padding(.vertical, isLandscape ? 24 : 32)
                .background(
                    Color.neonPanel.opacity(0.9)
                        .clipShape(RoundedRectangle(cornerRadius: 32, style: .continuous))
                )
                .overlay(
                    RoundedRectangle(cornerRadius: 32, style: .continuous)
                        .stroke(
                            LinearGradient(
                                colors: [
                                    accent.opacity(0.15),
                                    accent.opacity(0.7),
                                    accent.opacity(0.15)
                                ],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            ),
                            lineWidth: 1
                        )
                )
                .padding(.horizontal, isLandscape ? 16 : 24)
                .padding(.vertical, isLandscape ? 24 : 16)
                
                Spacer(minLength: 0)
            }
        }
    }
}

struct NeonTimeText: View {
    let text: String
    let accent: Color
    let isLandscape: Bool
    
    var body: some View {
        let fontSize: CGFloat = isLandscape ? 50 : 40
        
        Text(text)
            .font(.system(size: fontSize, weight: .heavy, design: .monospaced))
            .foregroundStyle(
                LinearGradient(
                    colors: [
                        accent,
                        Color.neonCyan,
                        Color.neonPink
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .shadow(color: accent.opacity(0.9),
                    radius: 15, x: 0, y: 0)
            .tracking(4)
    }
}

struct DividerNeonVertical: View {
    var body: some View {
        Rectangle()
            .fill(
                LinearGradient(
                    colors: [
                        Color.neonPink.opacity(0.1),
                        Color.neonCyan.opacity(0.8),
                        Color.neonPink.opacity(0.1)
                    ],
                    startPoint: .top,
                    endPoint: .bottom
                )
            )
            .frame(width: 3)
            .ignoresSafeArea()
    }
}

struct DividerNeonHorizontal: View {
    var body: some View {
        Rectangle()
            .fill(
                LinearGradient(
                    colors: [
                        Color.neonCyan.opacity(0.1),
                        Color.neonPink.opacity(0.8),
                        Color.neonCyan.opacity(0.1)
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
            .frame(height: 3)
            .ignoresSafeArea()
    }
}

// MARK: - 프리뷰
struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            ContentView()           // 기본 (가로/세로는 Preview에서 디바이스 회전으로 확인)
        }
    }
}
