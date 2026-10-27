import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App";
import { AuthProvider } from "./core/context/AuthContext";
import ErrorBoundary from "./core/components/ErrorBoundary";
createRoot(document.getElementById("root")).render(<StrictMode>
    <ErrorBoundary>
      <AuthProvider>
        <App />
      </AuthProvider>
    </ErrorBoundary>
  </StrictMode>);
