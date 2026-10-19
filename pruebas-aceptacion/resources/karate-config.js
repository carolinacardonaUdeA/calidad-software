function fn() {
  // Configuracion global de Karate. No hay URLs: se prueba la aplicacion por sus
  // puntos de entrada reales (motor, presenter de la UI y consola), no por HTTP.
  karate.configure('logPrettyRequest', false);
  return {
    pkg: 'com.udea.calidad.calculadora.acceptance'
  };
}
