
## Crypto

Crypto using async keys (encode, decode, sign, verifySignature)

See interface [Crypto](https://github.com/greetgo/greetgo.security/blob/master/src/kz/greetgo/security/crypto/Crypto.java)

```java

class Probe {
  public static void main(String[] args){
    
    //if you have some secret data with small or big amount of bytes (limits by memory)
    
    byte[] secretData = getSecretData();
    
    // and you have Crypto-interface
    
    Crypto crypto = getCrypto();
    
    // you can encrypt data
    
    byte[] encryptedData = crypto.encrypt(secretData);
    
    // and then decrypt data
    
    byte[] originalSecretData = crypto.decrypt(encryptedData);
    
    // be sure that secretData, originalSecretData contains the same bytes
    
    
  }
}

```

To create Crypto-interface implementation you can use CryptoBuilder. This builder may create implementation storing
key pair in:

 - Files in HDD (or SSD)
 - Relational database
   - PostgreSQL
   - Oracle
 - MongoDB

### Crypto-implementation in Relational Database

To create Crypto-implementation storing keys in relational database you can do following:

```java
class CryptoCreatorInRelationalDatabase {

  public static void createCrypto(){
    
    javax.sql.DataSource dataSource = getDataSource();
    
    Jdbc jdbc = new kz.greetgo.db.AbstractJdbcWithDataSource() {
      @Override
      protected DataSource getDataSource() {
        return dataSource;
      }
    
      @Override
      protected TransactionManager getTransactionManager() {
        return null;//May be null
      }
    };
    
    Crypto crypto = SecurityBuilders
      .newCryptoBuilder()
      
      .setKeySize(1024)
      
      .inDb(DbType.PostgreSQL, jdbc)              //May be Oracle
      .setConfig(new CryptoSourceConfigDefault()) //defines crypto algorithms (default: RSA)
      
      .setTableName("crypto_keys")      // table to store keys
      .setIdFieldLength(70)             // id field length
      .setIdFieldName("key_id")         // id field name
      .setValueFieldName("key_content") // field to contain key bytes
      
      .setPrivateKeyIdValue("private_key_id") // value of id for private key 
      .setPublicKeyIdValue("public_key_id")   // value of id for public key
      
      .build();
    
    //here you can use crypto
  }

}

```

The table `crypto_keys` will be automatically created when needed.

### Crypto-implementation in files

To create Crypto-implementation storing keys in files you can do following:

```java
class CryptoCreatorInFiles {

  public static void createCrypto(){
    
    // All files and directories will be create automatically
    
    final String keysDir = "build/test_data/CryptoTest/keys";
    
    File privateKeyFile = new File(keysDir + "/private.key");
    File publicKeyFile = new File(keysDir + "/public.key");
    
    Crypto crypto = SecurityBuilders
      .newCryptoBuilder()
      .setKeySize(1024)
      .inFiles(privateKeyFile, publicKeyFile)
      .setConfig(new CryptoSourceConfigDefault())
      .build();
    
    //here you can use crypto
  }

}
```

### Crypto-implementation in MongoDB

```java
class CryptoCreatorInMongoDB {

  public static void createCrypto(){
    
    com.mongodb.client.MongoCollection<org.bson.Document> collection = getCollection();
    
    Crypto crypto = SecurityBuilders
      .newCryptoBuilder()
      .setKeySize(1024)
      .setKeySize(keySize)
      .setConfig(new CryptoSourceConfigDefault())
      .inMongo(collection)
      .setPublicKeyFieldName("public_key_content")
      .setPrivateKeyFieldName("private_key_content")
      .setIdFieldName("key_id")
      .setPrivateId("cool_key")
      .setPublicId("cool_key")
      .build();
    
    //here you can use crypto
  }

}
```
