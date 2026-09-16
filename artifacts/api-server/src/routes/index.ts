import { Router, type IRouter } from "express";
import healthRouter from "./health";
import serverSelectorRouter from "./server-selector";

const router: IRouter = Router();

router.use(healthRouter);
router.use(serverSelectorRouter);

export default router;
