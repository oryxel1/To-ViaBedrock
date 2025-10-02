package oxy.toviabedrock.session.storage;

import lombok.RequiredArgsConstructor;
import oxy.toviabedrock.session.UserSession;

@RequiredArgsConstructor
public class UserStorage {
    protected final UserSession session;
}
