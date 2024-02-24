mod address_repository;

use std::str::FromStr;
use log::info;
use simple_logger::SimpleLogger;
use sqlx::postgres::{PgConnectOptions, PgPoolOptions};
use sqlx::ConnectOptions;
use structopt::StructOpt;
use crate::address_repository::AddressRepository;

#[derive(Debug, StructOpt)]
#[structopt(name = "address_service", about = "An Simple Address GRPC service")]
struct Opt {
  #[structopt(long, env = "DATABASE_URL")]
  db_url: String,

  #[structopt(long, default_value = "10")]
  db_max_connection: u32
}

#[tokio::main(flavor = "current_thread")]
async fn main() -> Result<(), sqlx::Error> {
  SimpleLogger::new().with_level(log::LevelFilter::Debug).init().unwrap();

  let opt = Opt::from_args();
  info!(target: "main", "db_url: {}", opt.db_url);

  let repository = AddressRepository::new(
    PgPoolOptions::new()
      .max_connections(opt.db_max_connection)
      .connect_with(PgConnectOptions::from_str(&opt.db_url)?.log_statements(log::LevelFilter::Trace))
      .await?
  );

  info!(target: "main", "result: {}", repository.get_address(7i64).await?);

  Ok(())
}


