package org.infinispan.invalidation;

import org.infinispan.Cache;
import org.infinispan.commands.write.WriteCommand;
import org.infinispan.test.AbstractCacheTest;
import org.infinispan.test.ReplListener;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "invalidation.AsyncAPIAsyncInvalTest")
public class AsyncAPIAsyncInvalTest extends AsyncAPISyncInvalTest {
   ReplListener rl;

   public AsyncAPIAsyncInvalTest() {
      cleanup = AbstractCacheTest.CleanupPhase.AFTER_METHOD;
   }

   @Override
   protected void createCacheManagers() throws Throwable {
      super.createCacheManagers();
      Cache c2 = cache(1, super.getClass().getSimpleName());
      rl = new ReplListener(c2, true);
   }

   @Override
   protected boolean sync() {
      return false;
   }
   
   @AfterClass(alwaysRun=true)
   protected void destroy() {     
      super.destroy();
      rl = null;
   }

   @Override
   protected void resetListeners() {
      rl.resetEager();
   }

   @Override
   protected void asyncWait(Class<? extends WriteCommand>... cmds) {
      if (cmds == null || cmds.length == 0)
         rl.expectAny();
      else
         rl.expect(cmds);

      rl.waitForRpc();
   }
}
