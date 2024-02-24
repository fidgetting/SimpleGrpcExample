use sqlx::Postgres;
use sqlx::types::JsonValue;
use std::io::Result;
use std::str::FromStr;
use protobuf_json_mapping::{print_to_string, parse_from_str};

include!(concat!(env!("OUT_DIR"), "/protos/mod.rs"));

use address::Address;

struct AddressRecord {
  address: JsonValue
}

pub struct AddressRepository {
  pool: sqlx::Pool<Postgres>
}

impl AddressRepository {

  pub const fn new(pool: sqlx::Pool<Postgres>) -> AddressRepository {
    Self { pool }
  }

  pub async fn get_address(self, id: i64) -> Result<Address> {
    AddressRepository::from_db(
      sqlx::query_as!(
        AddressRecord,
        r#"SELECT address FROM address WHERE id = $1"#,
        id
      ).fetch_one(&self.pool).await.expect("Error getting Address")
    )
  }

  pub async fn set_address(self, id: i64, address: Address) -> Result<Address> {
    AddressRepository::from_db(
      sqlx::query_as!(
        AddressRecord,
        r#"
          INSERT INTO address (id, address)
            VALUES ($1, $2)
          ON CONFLICT (id) DO UPDATE
            SET address = excluded.address
          RETURNING address
        "#,
        id,
        AddressRepository::to_db(&address)
      ).fetch_one(&self.pool).await.expect("Error setting Address")
    )
  }

  pub async fn create_address(self, address: Address) -> Result<Address> {
    AddressRepository::from_db(
      sqlx::query_as!(
        AddressRecord,
        r#"
          INSERT INTO address (address)
            VALUES ($1)
          RETURNING address
        "#,
        AddressRepository::to_db(&address)
      ).fetch_one(&self.pool).await.expect("Error setting Address")
    )
  }

  pub async fn remove_address(self, id: i64) -> Result<Address> {
    AddressRepository::from_db(
      sqlx::query_as!(
        AddressRecord,
        r#"
          DELETE FROM address
            WHERE
        "#
      )
    )
  }

  fn from_db(record: AddressRecord) -> Result<Address> {
    Ok(parse_from_str(&record.address.to_string()).expect("Unable to parse Address json"))
  }

  fn to_db(address: &Address) -> JsonValue {
    JsonValue::from_str(
      &print_to_string(address).expect("Unable to serialize Address to JSON")
    ).expect("Unable to parser serialized Address")
  }
}
