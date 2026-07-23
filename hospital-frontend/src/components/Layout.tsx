import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import Footer from "./Footer";
import { C } from "../core/theme";

export default function Layout() {
  return (
    <div
      style={{
        backgroundColor: C.white,
        fontFamily: "'Poppins', system-ui, sans-serif",
      }}
    >
      <Navbar />
      <main>
        <Outlet />
      </main>
      <Footer />
    </div>
  );
}
