/*******************************************************************************
 * This file is part of the Java SwingPrintPreview Library
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 René Bergelt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package de.renber.swing.controls.documentview.threading;

/**
 * A class copying .NET's AutoResetEvent
 * Used for thread synchronization (barrier)
 * @see http://stackoverflow.com/questions/1091973/javas-equivalent-to-nets-autoresetevent
 */
public class AutoResetEvent {

    private final Object _monitor = new Object();
    private volatile boolean _isOpen = false;

    public AutoResetEvent(boolean open) {
        _isOpen = open;
    }

    public void waitOne() throws InterruptedException {
        synchronized (_monitor) {
            while (!_isOpen) {
                _monitor.wait();
            }
            _isOpen = false;
        }
    }

    public void waitOne(long timeout) throws InterruptedException {
        synchronized (_monitor) {
            long t = System.currentTimeMillis();
            while (!_isOpen) {
                _monitor.wait(timeout);
                // Check for timeout
                if (System.currentTimeMillis() - t >= timeout) {
                    break;
                }
            }
            _isOpen = false;
        }
    }

    public void set() {
        synchronized (_monitor) {
            _isOpen = true;
            _monitor.notify();
        }
    }

    public void reset() {
        _isOpen = false;
    }
}
