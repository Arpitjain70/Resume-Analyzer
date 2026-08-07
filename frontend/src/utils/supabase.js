/**
 * ─────────────────────────────────────────────────────────
 * utils/supabase.js — Supabase Client Instance
 * Location: src/utils/supabase.js
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Creates a single shared Supabase client to be reused
 * across the entire app. Importing it multiple times always
 * returns the same instance (module singleton pattern).
 *
 * HOW TO USE:
 *   import { supabase } from '../utils/supabase';
 *
 *   // Example: query a table
 *   const { data, error } = await supabase.from('resumes').select('*');
 *
 * ENV VARIABLES (set in .env at the project root):
 *   VITE_SUPABASE_URL     → Your project URL from Supabase dashboard
 *   VITE_SUPABASE_ANON_KEY → Your public anon key from Supabase dashboard
 * ─────────────────────────────────────────────────────────
 */

import { createClient } from '@supabase/supabase-js';

const supabaseUrl = import.meta.env.VITE_SUPABASE_URL;
const supabasePublishableKey = import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY;

if (!supabaseUrl || !supabasePublishableKey) {
  console.warn(
    '[Supabase] Missing env variables. ' +
    'Set VITE_SUPABASE_URL and VITE_SUPABASE_PUBLISHABLE_KEY in your .env file.'
  );
}

export const supabase = createClient(supabaseUrl, supabasePublishableKey);
