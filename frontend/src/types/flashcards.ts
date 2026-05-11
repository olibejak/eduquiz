export interface DeckProgressSummary {
    id: number;
    title: string;
}

export interface DeckProgressStatus {
    title: string;
    dueCount: number;
    newCount: number;
    learnedCount: number;
    totalCount: number;
}

export type FlashcardRating = 'AGAIN' | 'HARD' | 'GOOD' | 'EXCELLENT';

export interface FlashcardReview {
    questionId: number;
    rating: FlashcardRating;
}

export interface FlashcardReviewBatch {
    reviews: FlashcardReview[];
}

export interface AnswerPayload {
    type: string;
    isCorrect?: boolean;
    associate?: boolean;
    matchId?: number;
}

export interface Answer {
    id: number;
    text: string;
    type: string;
    payload: AnswerPayload;
}

export interface Question {
    id: number;
    text: string;
    questionType: string;
    answers: Answer[];
    duration: number;
}