package org.infinispan.persistence.jpa;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Version;

import org.infinispan.commons.io.ByteBuffer;

/**
 * Entity which should hold serialized metadata
 * 
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
@Entity
@Table(name = "`__ispn_metadata__`")
class MetadataEntity {
   public static final String EXPIRATION = "expiration";

   @EmbeddedId
   private MetadataEntityKey key;
   @Lob
   @Column(length = 65535)
   private byte[] keyBytes;
   @Lob
   @Column(length = 65535)
   private byte[] metadata;
   @Column(name = EXPIRATION)
   private long expiration; // to simplify query for expired entries
   @Version
   private int version;

   public MetadataEntity() {
   }

   public MetadataEntity(ByteBuffer key, ByteBuffer metadata, long expiration) {
      this.key = new MetadataEntityKey(key.getBuf());
      this.keyBytes = key.getBuf();
      if (metadata != null) {
         this.metadata = metadata.getBuf();
      }
      this.expiration = expiration < 0 ? Long.MAX_VALUE : expiration;
   }

   public MetadataEntityKey getKey() {
      return key;
   }

   public void setKey(MetadataEntityKey key) {
      this.key = key;
   }
   
   public byte[] getKeyBytes() {
      return keyBytes;
   }

   public void setKeyBytes(byte[] keyBytes) {
      this.keyBytes = keyBytes;
   }

   public byte[] getMetadata() {
      return metadata;
   }

   public void setMetadata(byte[] metadata) {
      this.metadata = metadata;
   }

   public long getExpiration() {
      return expiration;
   }

   public void setExpiration(long expiration) {
      this.expiration = expiration;
   }

   public int getVersion() {
      return version;
   }

   public void setVersion(int version) {
      this.version = version;
   }

   public boolean hasBytes() {
      return metadata != null;
   }

}
