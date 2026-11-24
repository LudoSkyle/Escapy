// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_domain",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_domain",
      type: .none,
      targets: ["_domain"]
    )
  ],
  dependencies: [
  ],
  targets: [
    .target(
      name: "_domain",
      dependencies: [
      ]
    )
  ]
)
