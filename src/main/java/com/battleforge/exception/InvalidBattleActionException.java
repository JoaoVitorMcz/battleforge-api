package com.battleforge.exception;

/**
 * The action is well formed but not accepted by the battle's current phase,
 * e.g. a move submitted while the battle awaits a replacement for a fainted Pokemon.
 */
public class InvalidBattleActionException extends RuntimeException {

    public InvalidBattleActionException(String message) {
        super(message);
    }
}
