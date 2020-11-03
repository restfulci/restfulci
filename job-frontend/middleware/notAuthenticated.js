/*
 * https://nuxtjs.org/examples/auth-external-jwt
 */

 export default function ({ store, redirect }) {
   // If the user is authenticated redirect to home page
   if (store.state.auth) {
     return redirect('/jobs')
   }
 }
