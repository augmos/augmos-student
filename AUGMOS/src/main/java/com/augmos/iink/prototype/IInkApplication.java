// Copyright MyScript. All rights reserved.

package com.augmos.iink.prototype;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import com.augmos.certificate.MyCertificate;
import com.myscript.iink.Engine;

public class IInkApplication extends MultiDexApplication
{
  private static Engine engine;

  public static synchronized Engine getEngine()
  {
    if (engine == null)
    {
      engine = Engine.create(MyCertificate.getBytes());
    }

    return engine;
  }

}
