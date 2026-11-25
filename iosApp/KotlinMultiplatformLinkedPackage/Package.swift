// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/google/GoogleSignIn-iOS.git",
      from: "10.1.0"
    ),
    .package(path: "subpackages/dev_gitlive_firebase_auth_3_0_0_beta1"),
    .package(path: "subpackages/dev_gitlive_firebase_firestore_3_0_0_beta1"),
    .package(path: "subpackages/dev_gitlive_firebase_functions_3_0_0_beta1"),
    .package(path: "subpackages/dev_gitlive_firebase_storage_3_0_0_beta1"),
    .package(path: "subpackages/dev_gitlive_firebase_app_3_0_0_beta1")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(
          name: "GoogleSignIn",
          package: "GoogleSignIn-iOS"
        ),
        .product(name: "dev_gitlive_firebase_auth_3_0_0_beta1", package: "dev_gitlive_firebase_auth_3_0_0_beta1"),
        .product(name: "dev_gitlive_firebase_firestore_3_0_0_beta1", package: "dev_gitlive_firebase_firestore_3_0_0_beta1"),
        .product(name: "dev_gitlive_firebase_functions_3_0_0_beta1", package: "dev_gitlive_firebase_functions_3_0_0_beta1"),
        .product(name: "dev_gitlive_firebase_storage_3_0_0_beta1", package: "dev_gitlive_firebase_storage_3_0_0_beta1"),
        .product(name: "dev_gitlive_firebase_app_3_0_0_beta1", package: "dev_gitlive_firebase_app_3_0_0_beta1")
      ]
    )
  ]
)
