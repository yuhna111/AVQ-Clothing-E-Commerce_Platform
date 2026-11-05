import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { api, money } from "../api.js";
import { useApp } from "../state.jsx";

// UC08 xem và sửa giỏ (FR14, FR15)
export default function Cart() {
  const { session, notify, setCartCount } = useApp();
  const nav = useNavigate();
  const [cart, setCart] = useState(null);
  useEffect(() => { if (session) api("/cart").then(setCart).catch(() => setCart({ items: [] })); }, [session]);
  if (!session) return <div className="panel">Vui lòng <Link to="/dang-nhap?next=/gio-hang">đăng nhập</Link> để xem giỏ hàng.</div>;
  if (!cart) return <p className="muted">Đang tải...</p>;

  const update = async (path, opts) => {
    try { const c = await api(path, opts); setCart(c); setCartCount(c.totalQuantity); } catch (e) { notify(e.message, "err"); }
  };
  if (!cart.items.length) return <div className="panel">Giỏ hàng trống. <Link to="/">Tiếp tục mua sắm</Link></div>;
  return (
    <div className="two">
      <div>
        <h1>Giỏ hàng</h1>
        <table>
          <thead><tr><th></th><th>Sản phẩm</th><th>Đơn giá</th><th>Số lượng</th><th>Thành tiền</th><th></th></tr></thead>
          <tbody>
            {cart.items.map((i) => (
              <tr key={i.variantId}>
                <td><img src={i.image} width="56" style={{ borderRadius: 6 }} /></td>
                <td><Link to={"/san-pham/" + i.productId}><b>{i.productName}</b></Link><div className="muted">{i.size} / {i.color} · còn {i.stock}</div></td>
                <td>{money(i.unitPrice)}</td>
                <td>
                  <div className="qty">
                    <button onClick={() => i.quantity > 1 && update(`/cart/items/${i.variantId}`, { method: "PATCH", params: { qty: i.quantity - 1 } })}>−</button>
                    <span>{i.quantity}</span>
                    <button onClick={() => update(`/cart/items/${i.variantId}`, { method: "PATCH", params: { qty: i.quantity + 1 } })}>+</button>
                  </div>
                </td>
                <td className="price">{money(i.lineTotal)}</td>
                <td><a onClick={() => update(`/cart/items/${i.variantId}`, { method: "DELETE" })}>Xóa</a></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="panel summary">
        <h2>Tóm tắt</h2>
        <div className="line"><span>Tạm tính ({cart.totalQuantity} sản phẩm)</span><b>{money(cart.subtotal)}</b></div>
        <p className="muted">Miễn phí giao hàng cho đơn từ 500.000 đ; dưới mức này phí 30.000 đ (BR09).</p>
        <button className="btn" style={{ width: "100%" }} onClick={() => nav("/thanh-toan")}>ĐẶT HÀNG</button>
      </div>
    </div>
  );
}
