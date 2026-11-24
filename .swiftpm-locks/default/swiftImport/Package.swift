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
    .package(path: "subpackages/_data"),
    .package(path: "subpackages/_domain"),
    .package(path: "subpackages/_shared"),
    .package(path: "subpackages/dev_gitlive_firebase_app_3_0_0_beta1"),
    .package(path: "subpackages/dev_gitlive_firebase_auth_3_0_0_beta1"),
    .package(path: "subpackages/dev_gitlive_firebase_firestore_3_0_0_beta1"),
    .package(path: "subpackages/dev_gitlive_firebase_functions_3_0_0_beta1"),
    .package(path: "subpackages/dev_gitlive_firebase_storage_3_0_0_beta1")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "_data", package: "_data"),
        .product(name: "_domain", package: "_domain"),
        .product(name: "_shared", package: "_shared"),
        .product(name: "dev_gitlive_firebase_app_3_0_0_beta1", package: "dev_gitlive_firebase_app_3_0_0_beta1"),
        .product(name: "dev_gitlive_firebase_auth_3_0_0_beta1", package: "dev_gitlive_firebase_auth_3_0_0_beta1"),
        .product(name: "dev_gitlive_firebase_firestore_3_0_0_beta1", package: "dev_gitlive_firebase_firestore_3_0_0_beta1"),
        .product(name: "dev_gitlive_firebase_functions_3_0_0_beta1", package: "dev_gitlive_firebase_functions_3_0_0_beta1"),
        .product(name: "dev_gitlive_firebase_storage_3_0_0_beta1", package: "dev_gitlive_firebase_storage_3_0_0_beta1")
      ]
    )
  ]
)
