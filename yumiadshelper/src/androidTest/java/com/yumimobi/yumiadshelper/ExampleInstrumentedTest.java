package com.yumimobi.yumiadshelper;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented helper_native_model_01, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under helper_native_model_01.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.yumimobi.yumiadshelper.helper_native_model_01", appContext.getPackageName());
    }
}
