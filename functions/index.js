const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

// 1. Notificar al Admin cuando un profesor crea una reserva
exports.notificarAdminNuevaReserva = functions.firestore
    .document('reservas/{reservaId}')
    .onCreate(async (snap, context) => {
        const reserva = snap.data();
        const turnoReserva = reserva.turno; // Ejemplo: "Matutino"

        // Buscamos al admin en la colección 'roles' que coincida con el horario
        const adminsSnapshot = await admin.firestore().collection('roles')
            .where('admin', '==', true)
            .where('Horario', '==', turnoReserva)
            .get();

        const envios = [];
        adminsSnapshot.forEach(doc => {
            const adminData = doc.data();
            // IMPORTANTE: El admin debe tener el campo fcmToken en su documento
            if (adminData.fcmToken) {
                const message = {
                    notification: {
                        title: '⚠️ Nueva reserva solicitada',
                        body: `Profesor: ${reserva.profesorName} para el ${reserva.aula}`
                    },
                    token: adminData.fcmToken
                };
                envios.push(admin.messaging().send(message));
            }
        });
        return Promise.all(envios);
    });

// 2. Notificar al Profesor cuando el Admin acepta o rechaza
exports.notificarRespuestaProfesor = functions.firestore
    .document('reservas/{reservaId}')
    .onUpdate(async (change, context) => {
        const dataNueva = change.after.data();
        const dataVieja = change.before.data();

        // Solo actuamos si el status cambió (ej: de "Pendiente" a "Aceptada")
        if (dataNueva.status !== dataVieja.status) {
            const profesorID = dataNueva.ProfesorUID;

            // Buscamos el token en la colección 'profesores'
            const profDoc = await admin.firestore().collection('profesores').doc(profesorID).get();
            
            if (profDoc.exists && profDoc.data().fcmToken) {
                const message = {
                    notification: {
                        title: `Reserva ${dataNueva.status}`,
                        body: `Tu solicitud para el aula ${dataNueva.aula} ha sido ${dataNueva.status.toLowerCase()}.`
                    },
                    token: profDoc.data().fcmToken
                };
                return admin.messaging().send(message);
            }
        }
        return null;
    });