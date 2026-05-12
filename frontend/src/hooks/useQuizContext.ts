import {useOutletContext} from "react-router-dom";
import type {useQuizWebSocket} from "./useQuizWebSocket.ts";

export function useQuizContext() {
    return useOutletContext<{
        ws: ReturnType<typeof useQuizWebSocket>;
        isHost: boolean;
        participantId: number;
        participantToken: string;
        isHostPlaying: boolean;
        setIsHostPlaying: (val: boolean) => void;
        nicknames: Record<number, string>;
        setNicknames: (val: Record<number, string>) => void;
    }>();
}
