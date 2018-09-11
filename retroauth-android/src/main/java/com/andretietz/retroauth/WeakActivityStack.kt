/*
 * Copyright (c) 2016 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andretietz.retroauth

import android.app.Activity
import android.util.SparseArray

import java.lang.ref.WeakReference
import java.util.LinkedList

internal class WeakActivityStack {

  private val map = SparseArray<WeakReference<Activity>>()

  private val stack = LinkedList<Int>()

  fun push(item: Activity) {
    val identifier = getIdentifier(item)
    synchronized(this) {
      stack.push(identifier)
      map.put(identifier, WeakReference(item))
    }
  }

  fun pop(): Activity? {
    synchronized(this) {
      if (!stack.isEmpty()) {
        val identifier = stack.removeFirst()
        val item = map.get(identifier!!).get()
        map.remove(identifier)
        return item
      }
      return null
    }
  }

  fun remove(item: Activity) {
    val identifier = getIdentifier(item)
    synchronized(this) {
      stack.remove(identifier)
      map.remove(identifier)
    }
  }

  fun peek(): Activity? {
    synchronized(this) {
      if (!stack.isEmpty()) {
        return map.get(stack.first).get()
      }
    }
    return null
  }

  private fun getIdentifier(item: Activity): Int {
    return item.hashCode()
  }

}
