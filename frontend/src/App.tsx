import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import {AuthProvider} from "./context/AuthContext.tsx";
import Home from "./pages/Home.tsx"

// TMP page components
const QuizLobby = () => <div><h1 className="text-2xl font-bold">Kvíz</h1><p>Připojit se nebo vytvořit místnost...</p></div>;
const FlashcardsLobby = () => <div><h1 className="text-2xl font-bold">Flashcards</h1></div>;
const Library = () => <div><h1 className="text-2xl font-bold">Knihovna sad</h1></div>;
const CreateDeck = () => <div><h1 className="text-2xl font-bold">Tvorba sady</h1></div>;

function App() {
  return (
      <AuthProvider>
          <BrowserRouter>
            <Routes>
              {/* Pages */}
              <Route path="/" element={<Layout />}>
                <Route index element={<Home />} />
                <Route path="quiz" element={<QuizLobby />} />
                <Route path="flashcards" element={<FlashcardsLobby />} />
                <Route path="library" element={<Library />} />
                <Route path="create" element={<CreateDeck />} />
              </Route>

              {/* -> přidat cesty bez Layoutu, např. samotný probíhající kvíz (rozhraní otázky),
                který zabírá celou obrazovku bez levého menu */}
            </Routes>
          </BrowserRouter>
      </AuthProvider>
  );
}

export default App;
