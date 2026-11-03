const fs = require("fs");
const path = require("path");

const {
    getApps,
    initializeApp,
    cert,
    applicationDefault
} = require("firebase-admin/app");

const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

// Local development uses backend/firebase-service-account.json.
// Production should provide the service account as a Render Secret File and
// set FIREBASE_SERVICE_ACCOUNT_PATH to its mounted path.
const serviceAccountPath =
    process.env.FIREBASE_SERVICE_ACCOUNT_PATH ||
    path.resolve(__dirname, "..", "firebase-service-account.json");

let firebaseApp = getApps()[0];

if (!firebaseApp) {
    if (fs.existsSync(serviceAccountPath)) {
        const serviceAccount = JSON.parse(
            fs.readFileSync(serviceAccountPath, "utf8")
        );

        firebaseApp = initializeApp({
            credential: cert(serviceAccount),
            projectId: serviceAccount.project_id
        });

        console.log(
            `Firebase Admin connected to project: ${serviceAccount.project_id}`
        );
    } else if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
        firebaseApp = initializeApp({
            credential: applicationDefault(),
            projectId: process.env.FIREBASE_PROJECT_ID
        });

        console.log("Firebase Admin connected using Application Default Credentials");
    } else {
        throw new Error(
            "Firebase credentials not found. Configure FIREBASE_SERVICE_ACCOUNT_PATH."
        );
    }
}

const firestore = getFirestore(firebaseApp);
const messaging = getMessaging(firebaseApp);

module.exports = {
    firebaseApp,
    firestore,
    messaging,
    FieldValue
};
