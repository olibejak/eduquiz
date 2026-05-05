import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../api/axios';

interface QuizHistoryItem {
    id: number;
    date: string;
    rank: number;
}

interface FlashcardHistoryItem {
    id: number;
    deck: string;
    deckId: number;
}

export default function Home() {
    const { user } = useAuth();

    const [quizHistory, setQuizHistory] = useState<QuizHistoryItem[]>([]);
    const [flashcardHistory, setFlashcardHistory] = useState<FlashcardHistoryItem[]>([]);
    const [loading, setLoading] = useState(!!user);

    useEffect(() => {
        if (!user) {
            return;
        }

        const fetchData = async () => {
            try {
                setLoading(true);
                const [quizRes, flashcardRes] = await Promise.all([
                    api.get<QuizHistoryItem[]>('/history/quizzes'),
                    api.get<FlashcardHistoryItem[]>('/history/flashcards')
                ]);

                setQuizHistory(quizRes.data);
                setFlashcardHistory(flashcardRes.data);
            } catch (error) {
                console.error("Chyba při stahování historie", error);
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [user]);

    return (
        <div className="max-w-5xl">
            <h1 className="text-3xl font-bold text-gray-800 mb-8">
                {user ? user.username : 'Anonymní uživatel'}
            </h1>

            {/* Loading screen */}
            {loading ? (
                <div className="text-gray-500 animate-pulse text-lg">Načítám historii...</div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">

                    {/* Table: quiz history*/}
                    <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                        <h2 className="text-xl font-semibold mb-4 text-gray-700">Historie kvízů</h2>
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="border-b-2 border-gray-200">
                                <th className="py-2 px-4 font-medium text-gray-600">Datum</th>
                                <th className="py-2 px-4 font-medium text-gray-600">Umístění</th>
                            </tr>
                            </thead>
                            <tbody>
                            {quizHistory.map((item) => (
                                <tr key={item.id} className="border-b border-gray-100 last:border-0 hover:bg-gray-50">
                                    <td className="py-3 px-4 text-gray-800">{item.date}</td>
                                    <td className="py-3 px-4 font-medium text-blue-600">{item.rank}. místo</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                    {/* Table: flashcards history */}
                    <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-sm">
                        <h2 className="text-xl font-semibold mb-4 text-gray-700">Historie flashcards</h2>
                        <table className="w-full text-left border-collapse">
                            <thead>
                            <tr className="border-b-2 border-gray-200">
                                <th className="py-2 px-4 font-medium text-gray-600">Datum</th>
                                <th className="py-2 px-4 font-medium text-gray-600">Sada</th>
                            </tr>
                            </thead>
                            <tbody>
                            {flashcardHistory.map((item) => (
                                <tr key={item.id} className="border-b border-gray-100 last:border-0 hover:bg-gray-50">
                                    <td className="py-3 px-4 text-gray-800">{item.deck}</td>
                                </tr>
                            ))}
                            </tbody>
                        </table>
                    </div>

                </div>
            )}
        </div>
    );
}