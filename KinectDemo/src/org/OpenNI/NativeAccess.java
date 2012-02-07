/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.OpenNI;
 
import java.nio.ByteBuffer;
 
public class NativeAccess {
	static public void copyToBuffer(ByteBuffer buffer, long ptr, int size) {
		NativeMethods.copyToBuffer(buffer, ptr, size);
	}
};