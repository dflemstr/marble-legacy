package org.marble.physics;

import java.util.Set;

import com.bulletphysics.ContactDestroyedCallback;

import org.apache.commons.lang3.tuple.Pair;

import org.marble.entity.Collidable;
import org.marble.entity.Physical;

public final class CollContactDestroyed extends ContactDestroyedCallback {
    private final Set<Pair<Physical, Physical>> contacts;

    public CollContactDestroyed(final Set<Pair<Physical, Physical>> contacts) {
        this.contacts = contacts;
    }

    @Override
    public boolean contactDestroyed(final Object userPersistentData) {
        if (userPersistentData instanceof Pair<?, ?>) {
            final Pair<?, ?> pair = (Pair<?, ?>) userPersistentData;
            if (pair.getLeft() instanceof Physical
                    && pair.getRight() instanceof Physical) {
                @SuppressWarnings("unchecked")
                final Pair<Physical, Physical> contact =
                        (Pair<Physical, Physical>) pair;
                final Physical physical1 = contact.getLeft();
                final Physical physical2 = contact.getRight();

                if (contacts.contains(contact)) {
                    contacts.remove(contact);
                    if (physical1 instanceof Collidable) {
                        ((Collidable) physical1)
                                .handleContactRemoved(physical2);
                    }
                    if (physical2 instanceof Collidable) {
                        ((Collidable) physical2)
                                .handleContactRemoved(physical1);
                    }
                }
            }
        }
        return false;
    }
}
