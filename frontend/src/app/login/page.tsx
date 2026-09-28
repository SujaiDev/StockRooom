"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { Boxes, LoaderCircle, LockKeyhole } from "lucide-react";
import { api } from "@/services/api";

export default function LoginPage() {
  const router = useRouter();
  const [isSignup, setIsSignup] = useState(false);
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (isSignup) {
      setError("Registration is disabled for this single-user demo. Configure the database before adding accounts.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      if (email.trim().toLowerCase() === "admin" && password === "1234") {
        try {
          const result = await api.login(email, password);
          localStorage.setItem("stockroom_token", result.token);
          localStorage.removeItem("stockroom_demo_user");
        } catch {
          localStorage.setItem("stockroom_token", "local-demo-admin");
          localStorage.setItem("stockroom_demo_user", JSON.stringify({ name: "Demo Administrator", email: "admin", role: "INVENTORY_MANAGER" }));
        }
        router.replace("/");
        return;
      }
      const result = await api.login(email, password);
      localStorage.setItem("stockroom_token", result.token);
      localStorage.removeItem("stockroom_demo_user");
      router.replace("/");
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Could not sign in.");
    } finally {
      setBusy(false);
    }
  }

  return <main className="login-page"><div className="login-art"><div className="login-grid" /><div className="login-orbit orbit-one" /><div className="login-orbit orbit-two" /><div className="login-art-copy"><span className="eyebrow"><i className="live-dot" /> INVENTORY OPERATIONS</span><h1>Every item.<br /><em>Accounted for.</em></h1><p>One clear view of stock, movement, and the teams keeping goods in motion.</p><div className="login-stat-row"><span><strong>01</strong><small>One live ledger</small></span><span><strong>∞</strong><small>Room to grow</small></span></div></div><span className="login-art-foot">STOCKROOM / WAREHOUSE MANAGEMENT</span></div><section className="login-side"><div className="login-card"><div className="login-brand"><span className="brand-mark"><Boxes size={19} /></span><span>stockroom<span className="brand-period">.</span></span></div><p className="login-kicker">YOUR WORKSPACE IS READY</p><h2>{isSignup ? "Create your account" : "Welcome back"}</h2><p className="login-copy">{isSignup ? "Registration is paused until database setup." : "Sign in to continue to your inventory workspace."}</p>{!isSignup && <div className="demo-credentials"><strong>Local demo access</strong><span>Login ID <b>admin</b></span><span>Password <b>1234</b></span></div>}{isSignup && <div className="notice notice-info">Only the built-in demo administrator is enabled. Configure the database to create user accounts.</div>}<form className="login-form" onSubmit={submit}>{isSignup && <label>Your name<input autoComplete="name" value={name} onChange={(event) => setName(event.target.value)} required /></label>}<label>{isSignup ? "Email address" : "Login ID / email"}<input type={isSignup ? "email" : "text"} autoComplete={isSignup ? "email" : "username"} value={email} onChange={(event) => setEmail(event.target.value)} required /></label><label>Password<input type="password" autoComplete={isSignup ? "new-password" : "current-password"} minLength={isSignup ? 8 : undefined} value={password} onChange={(event) => setPassword(event.target.value)} required /></label>{error && <p role="alert" className="form-error">{error}</p>}<button className="button button-primary login-submit" type="submit" disabled={busy || isSignup}>{busy ? <LoaderCircle className="spin" size={16} /> : <LockKeyhole size={16} />}{isSignup ? "Registration unavailable" : "Sign in"}</button></form><p className="login-switch">{isSignup ? "Already have an account?" : "Need an account?"} <button onClick={() => { setIsSignup(!isSignup); setError(""); }}>{isSignup ? "Sign in" : "Sign up"}</button></p><div className="login-security"><LockKeyhole size={14} /> Demo access is local-only; configure the database for persistent accounts.</div></div></section></main>;
}