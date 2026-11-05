import React, { useState } from "react";
import { Link, NavLink, Route, Routes, useNavigate } from "react-router-dom";
import { useApp } from "./state.jsx";
import Home from "./pages/Home.jsx";
import ProductDetail from "./pages/ProductDetail.jsx";
import Cart from "./pages/Cart.jsx";
import Checkout from "./pages/Checkout.jsx";
import MockPay from "./pages/MockPay.jsx";
import Orders from "./pages/Orders.jsx";
import OrderDetail from "./pages/OrderDetail.jsx";
import { Login, Register } from "./pages/Auth.jsx";
import AdminLogin from "./admin/AdminLogin.jsx";
import AdminOrders from "./admin/AdminOrders.jsx";
import AdminProducts from "./admin/AdminProducts.jsx";

function Header() {
  const { session, logout, cartCount } = useApp();
  const nav = useNavigate();
  const [q, setQ] = useState("");
  const isStaff = session && session.role === "STAFF";
  return (
    <header className="header">
      <div className="wrap header-row">
        <Link to="/" className="logo">AVQ <span>CLOTHING</span></Link>
        {!isStaff && (
          <form className="search" onSubmit={(e) => { e.preventDefault(); nav("/?q=" + encodeURIComponent(q)); }}>
            <input placeholder="Tìm áo, quần, hoodie..." value={q} onChange={(e) => setQ(e.target.value)} />
            <button>Tìm</button>
          </form>
        )}
        <nav className="nav">
          {isStaff ? (
            <>
              <NavLink to="/admin/don-hang">Đơn hàng</NavLink>
              <NavLink to="/admin/san-pham">Sản phẩm</NavLink>
            </>
          ) : (
            <>
              <NavLink to="/gio-hang" className="cart-link">Giỏ hàng{cartCount > 0 && <b className="badge">{cartCount}</b>}</NavLink>
              {session && <NavLink to="/don-hang">Đơn của tôi</NavLink>}
            </>
          )}
          {session ? (
            <span className="user">{session.name} · <a onClick={() => { logout(); nav(isStaff ? "/admin" : "/"); }}>Đăng xuất</a></span>
          ) : (
            <NavLink to="/dang-nhap">Đăng nhập</NavLink>
          )}
        </nav>
      </div>
    </header>
  );
}

export default function App() {
  return (
    <>
      <Header />
      <main className="wrap main">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/san-pham/:id" element={<ProductDetail />} />
          <Route path="/gio-hang" element={<Cart />} />
          <Route path="/thanh-toan" element={<Checkout />} />
          <Route path="/thanh-toan-gia-lap" element={<MockPay />} />
          <Route path="/don-hang" element={<Orders />} />
          <Route path="/don-hang/:code" element={<OrderDetail />} />
          <Route path="/dang-nhap" element={<Login />} />
          <Route path="/dang-ky" element={<Register />} />
          <Route path="/admin" element={<AdminLogin />} />
          <Route path="/admin/don-hang" element={<AdminOrders />} />
          <Route path="/admin/san-pham" element={<AdminProducts />} />
        </Routes>
      </main>
      <footer className="footer"><div className="wrap">AVQ Clothing · Demo tiểu luận Phân tích thiết kế hệ thống thông tin · Nhân viên: <Link to="/admin">trang quản trị</Link></div></footer>
    </>
  );
}
