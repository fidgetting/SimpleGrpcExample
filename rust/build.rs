use std::io::Result;

fn main() -> Result<()> {

  protobuf_codegen::Codegen::new()
    .cargo_out_dir("protos")
    .include("src/protos")
    .input("src/protos/address.proto")
    .input("src/protos/address_service.proto")
    .run_from_script();

  println!("cargo:rerun-if-changed=build.rs");
  Ok(())
}