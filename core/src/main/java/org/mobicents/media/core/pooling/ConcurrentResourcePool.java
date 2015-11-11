/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag. 
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

package org.mobicents.media.core.pooling;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Concurrent implementation of a {@link ResourcePool} that is thread safe.
 * 
 * @author Henrique Rosa (henrique.rosa@telestax.com)
 *
 */
public class ConcurrentResourcePool<T extends PooledObject> implements ResourcePool<T> {

    private final Queue<T> resources;

    public ConcurrentResourcePool() {
        this.resources = new ConcurrentLinkedQueue<T>();
    }

    @Override
    public void offer(T resource) {
        resource.checkIn();
        this.resources.add(resource);
    }

    @Override
    public T poll() {
        T resource = this.resources.poll();
        if (resource != null) {
            resource.checkOut();
        }
        return resource;
    }

    @Override
    public int size() {
        return resources.size();
    }

    @Override
    public void clear() {
        this.resources.clear();
    }

}