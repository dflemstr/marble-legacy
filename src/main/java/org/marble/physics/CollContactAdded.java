package org.marble.physics;

import java.util.Set;

import com.bulletphysics.ContactAddedCallback;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.narrowphase.ManifoldPoint;

import org.apache.commons.lang3.tuple.Pair;

import org.marble.entity.Collidable;
import org.marble.entity.Physical;

public final class CollContactAdded extends ContactAddedCallback {
    private final Set<Pair<Physical, Physical>> contacts;

    public CollContactAdded(final Set<Pair<Physical, Physical>> contacts) {
        this.contacts = contacts;
    }

    @Override
    public boolean contactAdded(final ManifoldPoint manifold,
            final CollisionObject col1, final int part1, final int index1,
            final CollisionObject col2, final int part2, final int index2) {
        if (!(col1.getUserPointer() instanceof Physical && col2
                .getUserPointer() instanceof Physical))
            throw new IllegalStateException(
                    "Collision object user pointer does not point to physical entity");

        final Physical physical1 = (Physical) col1.getUserPointer();
        final Physical physical2 = (Physical) col2.getUserPointer();

        final Pair<Physical, Physical> contact;
        if (System.identityHashCode(physical1) < System
                .identityHashCode(physical2)) {
            contact = Pair.of(physical1, physical2);
        } else {
            contact = Pair.of(physical2, physical1);
        }

        manifold.userPersistentData = contact;
        if (!contacts.contains(contact)) {
            contacts.add(contact);

            if (physical1 instanceof Collidable) {
                ((Collidable) physical1).handleContactAdded(physical2);
            }

            if (physical2 instanceof Collidable) {
                ((Collidable) physical2).handleContactAdded(physical1);
            }
        }
        return false;
    }
}
