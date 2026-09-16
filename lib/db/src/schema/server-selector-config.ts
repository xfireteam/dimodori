import { boolean, pgTable, text, timestamp } from "drizzle-orm/pg-core";

export const serverSelectorConfigTable = pgTable("server_selector_config", {
  key: text("key").primaryKey(),
  hideServerSelector: boolean("hide_server_selector").notNull().default(false),
  updatedAt: timestamp("updated_at", { withTimezone: true })
    .notNull()
    .defaultNow(),
});

export type ServerSelectorConfig =
  typeof serverSelectorConfigTable.$inferSelect;