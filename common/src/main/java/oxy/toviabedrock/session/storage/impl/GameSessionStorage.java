package oxy.toviabedrock.session.storage.impl;

import lombok.Getter;
import lombok.Setter;
import oxy.toviabedrock.session.UserSession;
import oxy.toviabedrock.session.storage.UserStorage;

public class GameSessionStorage extends UserStorage {
    @Getter @Setter
    private boolean blockNetworkIdsHashed;

    public GameSessionStorage(UserSession session) {
        super(session);
    }
}
