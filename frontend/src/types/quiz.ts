export interface LobbyDeck {
    deckId: number;
    name: string;
}

export interface LobbyParticipant {
    id: number;
    nickname: string;
    role: string;
}

export interface LobbySnapshot {
    lobbyPin: string;
    state: string;
    participants: LobbyParticipant[];
    decks: LobbyDeck[];
}

export type ParticipantStatus = 'JOINED' | 'CONNECTED' | 'DISCONNECTED' | 'KICKED';
export type DeckChangeType = 'ADDED' | 'REMOVED';