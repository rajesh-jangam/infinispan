/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.infinispan.io;

import org.infinispan.Cache;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Bela Ban
 * @author Marko Luksa
 * @author Manik Surtani
 */
public class GridInputStream extends InputStream {

   private int index = 0;                // index into the file for writing
   private int localIndex = 0;
   private byte[] currentBuffer = null;
   private int fSize;
   private boolean streamClosed = false;
   private FileChunkMapper fileChunkMapper;

   GridInputStream(GridFile file, Cache<String, byte[]> cache) {
      fileChunkMapper = new FileChunkMapper(file, cache);
      fSize = (int)file.length();
   }

   @Override public int read() throws IOException {
      assertOpen();
      if (isEndReached())
         return -1;
      if (getBytesRemainingInChunk() == 0)
         getChunk();
      int retval = 0x0ff & currentBuffer[localIndex++];
      index++;
      return retval;
   }

   @Override
   public int read(byte[] b) throws IOException {
      return read(b, 0, b.length);
   }

   @Override
   public int read(byte[] bytes, int offset, int length) throws IOException {
      assertOpen();
      int totalBytesRead = 0;
      while (length > 0) {
         int bytesRead = readFromChunk(bytes, offset, length);
         if (bytesRead == -1)
            return totalBytesRead > 0 ? totalBytesRead : -1;
         offset += bytesRead;
         length -= bytesRead;
         totalBytesRead += bytesRead;
      }

      return totalBytesRead;
   }

   private int readFromChunk(byte[] b, int off, int len) {
      if (isEndReached())
         return -1;
      int remaining = getBytesRemainingInChunk();
      if (remaining == 0) {
         getChunk();
         remaining = getBytesRemainingInChunk();
      }
      int bytesToRead = Math.min(len, remaining);
      System.arraycopy(currentBuffer, localIndex, b, off, bytesToRead);
      localIndex += bytesToRead;
      index += bytesToRead;
      return bytesToRead;
   }

   @Override public long skip(long length) throws IOException {
      assertOpen();
      if (length <= 0)
         return 0;

      int bytesToSkip = Math.min((int)length, getBytesRemainingInStream());
      index += bytesToSkip;
      if (bytesToSkip <= getBytesRemainingInChunk()) {
         localIndex += bytesToSkip;
      } else {
         getChunk();
         localIndex = index % getChunkSize();
      }
      return bytesToSkip;
   }

   @Override
   public int available() throws IOException {
      assertOpen();
      return getBytesRemainingInChunk();  // Return bytes in chunk
   }

   @Override
   public void close() throws IOException {
      localIndex = index = 0;
      streamClosed = true;
   }

   private boolean isEndReached() {
      return index == fSize;
   }

   private void assertOpen() throws IOException{
       if (streamClosed) throw new IOException("Stream is closed");
   }

   private int getBytesRemainingInChunk() {
      return currentBuffer == null ? 0 : currentBuffer.length - localIndex;
   }

   private int getBytesRemainingInStream() {
      return fSize - index;
   }

   private void getChunk() {
      currentBuffer = fileChunkMapper.fetchChunk(getChunkNumber());
      localIndex = 0;
   }

   private int getChunkNumber() {
      return index / getChunkSize();
   }

   private int getChunkSize() {
      return fileChunkMapper.getChunkSize();
   }
}
