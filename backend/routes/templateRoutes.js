const express = require("express");

const {
    getTemplates,
    getTemplateFields
} = require("../controllers/templateController");

const router = express.Router();

router.get("/", getTemplates);

router.get("/:id/fields", getTemplateFields);

module.exports = router;