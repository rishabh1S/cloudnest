"use client";

import { Suspense } from "react";
import ResetPasswordPageInner from "./reset-password-inner";

export default function Page() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <ResetPasswordPageInner />
    </Suspense>
  );
}