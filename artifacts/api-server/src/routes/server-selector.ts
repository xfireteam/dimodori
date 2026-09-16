import { Router, type IRouter } from "express";
import { eq } from "drizzle-orm";
import { db, serverSelectorConfigTable } from "@workspace/db";
import {
  GetServerSelectorConfigResponse,
  UpdateServerSelectorConfigBody,
  UpdateServerSelectorConfigResponse,
} from "@workspace/api-zod";

const CONFIG_KEY = "default";
const router: IRouter = Router();

async function getOrCreateConfig() {
  await db
    .insert(serverSelectorConfigTable)
    .values({ key: CONFIG_KEY, hideServerSelector: false })
    .onConflictDoNothing();

  const [config] = await db
    .select()
    .from(serverSelectorConfigTable)
    .where(eq(serverSelectorConfigTable.key, CONFIG_KEY))
    .limit(1);

  if (!config) {
    throw new Error("Server selector configuration could not be loaded");
  }

  return config;
}

router.get("/server-selector", async (_req, res): Promise<void> => {
  const config = await getOrCreateConfig();
  res.json(GetServerSelectorConfigResponse.parse(config));
});

router.patch("/server-selector", async (req, res): Promise<void> => {
  const parsed = UpdateServerSelectorConfigBody.safeParse(req.body);
  if (!parsed.success) {
    req.log.warn(
      { errors: parsed.error.flatten() },
      "Invalid server selector configuration",
    );
    res.status(400).json({ error: "Invalid server selector configuration" });
    return;
  }

  const [config] = await db
    .insert(serverSelectorConfigTable)
    .values({
      key: CONFIG_KEY,
      hideServerSelector: parsed.data.hideServerSelector,
      updatedAt: new Date(),
    })
    .onConflictDoUpdate({
      target: serverSelectorConfigTable.key,
      set: {
        hideServerSelector: parsed.data.hideServerSelector,
        updatedAt: new Date(),
      },
    })
    .returning();

  res.json(UpdateServerSelectorConfigResponse.parse(config));
});

export default router;