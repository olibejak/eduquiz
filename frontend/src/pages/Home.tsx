import { useAuth } from '../context/AuthContext';
import DueDecksWidget from "../components/DueDecksWidget.tsx";
import QuizHistoryWidget from "../components/QuizHistoryWidget.tsx";

export default function Home() {
    const { user } = useAuth();

    return (
        <div className="max-w-5xl relative">
            {/* Header */}
            <h1 className="text-3xl font-bold text-(--text-h) mb-8 transition-colors leading-tight">
                {user ? (
                    user.username
                ) : (
                    <span className="inline-flex flex-col">
                        <span>Anonymní uživatel</span>
                        <span className="mt-2 text-base font-normal text-(--text)">
                            Pro zobrazení historie kvízů a ukládání postupu flashcards se přihlaste.
                        </span>
                    </span>
                )}
            </h1>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Quiz history - Widget */}
                {user && (
                    <QuizHistoryWidget />
                )}

                {/* Flashcards decks to study - Widget */}
                <div className="md:col-span-1">
                    {user && (
                        <DueDecksWidget />
                    )}
                </div>
            </div>
        </div>
    );
}