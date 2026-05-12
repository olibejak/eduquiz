import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import {AuthProvider} from "./context/AuthContext.tsx";
import Home from "./pages/Home.tsx"
import Settings from "./pages/Settings.tsx";
import Library from "./pages/Library.tsx";
import CreateDeck from "./pages/CreateDeck.tsx";
import EditDeck from "./pages/EditDeck.tsx";
import {GoogleOAuthProvider} from "@react-oauth/google";
import ProtectedRoute from './components/ProtectedRoute';
import FlashcardDashboard from "./pages/flashcards/FlashcardsDashboard.tsx";
import StudyDeckIntro from "./pages/flashcards/StudyDeckIntro.tsx";
import StudySession from "./pages/flashcards/StudySession.tsx";
import QuizEntry from "./pages/quiz/QuizEntry.tsx";
import QuizLobby from "./pages/quiz/QuizLobby.tsx";
import QuizPlay from "./pages/quiz/QuizPlay.tsx";
import QuizLayout from "./components/QuizLayout.tsx";
import Help from "./pages/Help.tsx";

function App() {
    return (
        <AuthProvider>
            <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
                <BrowserRouter>
                    <Routes>
                        {/* Layout */}
                        <Route path="/" element={<Layout />}>

                            {/* Public Pages */}
                            <Route index element={<Home />} />
                            <Route path="library" element={<Library />} />
                            <Route path="/quiz" element={<QuizEntry />} />
                            <Route path="/flashcards" element={<FlashcardDashboard />} />
                            <Route path="/flashcards/:id" element={<StudyDeckIntro />} />
                            <Route path="/flashcards/:id/study" element={<StudySession />} />
                            <Route path="/help" element={<Help />} />

                            <Route path="/quiz/:pin" element={<QuizLayout />}>
                                <Route path="lobby" element={<QuizLobby />} />
                                <Route path="play" element={<QuizPlay />} />
                            </Route>

                            {/* Private Pages */}
                            <Route element={<ProtectedRoute />}>
                                <Route path="create" element={<CreateDeck />} />
                                <Route path="/edit/:id" element={<EditDeck />} />
                                <Route path="settings" element={<Settings />} />
                            </Route>
                        </Route>
                    </Routes>
                </BrowserRouter>
            </GoogleOAuthProvider>
        </AuthProvider>
    );
}

export default App;
