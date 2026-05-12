export interface DeckSummary {
    id: number;
    title: string;
    description: string;
    authorName: string;
    visibility: VisibilityType;
    tags: string[];
    favoritesCount: number;
    isFavorite?: boolean;
    numberOfQuestions: number;
    createdAt: string;
    updatedAt: string;
}

export const DECK_TAGS = [
    { id: 'VERIFIED', label: 'Ověřeno' },
    { id: 'SCIENCE', label: 'Věda' },
    { id: 'HISTORY', label: 'Dějepis' },
    { id: 'GEOGRAPHY', label: 'Zeměpis' },
    { id: 'MATHEMATICS', label: 'Matematika' },
    { id: 'LANGUAGES', label: 'Jazyky' },
    { id: 'PROGRAMMING', label: 'Programování' },
    { id: 'LITERATURE', label: 'Literatura' },
    { id: 'POP_CULTURE', label: 'Popkultura' },
    { id: 'ART', label: 'Umění' },
    { id: 'OTHER', label: 'Ostatní' }
];

export type VisibilityType = 'PUBLIC' | 'PRIVATE';

export type QuestionType = 'MULTIPLE_CHOICE' | 'WRITE' | 'NUMERIC' | 'MATCHING';
export type AnswerType = 'STANDARD' | 'CHOICE' | 'MATCHING';

export interface ChoicePayload {
    isCorrect: boolean;
}

export interface MatchingPayload {
    associate: boolean;
    matchId: number;
}

export interface AnswerState {
    id?: number;
    text: string;
    type: AnswerType;
    payload: ChoicePayload | MatchingPayload | null;
}

export interface QuestionState {
    id?: number;
    text: string;
    questionType: QuestionType;
    duration: number;
    answers: AnswerState[];
    isLocked: boolean;
}