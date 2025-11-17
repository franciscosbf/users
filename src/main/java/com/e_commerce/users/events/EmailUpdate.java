package com.e_commerce.users.events;

import java.io.Serial;
import java.io.Serializable;

public record EmailUpdate(String username,
                          String email) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
