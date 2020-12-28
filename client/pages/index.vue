<template>
  <v-form v-model="valid">
    <v-container>
      <h1 class="display-4" align="center">Tic Tac Toe</h1>
      <h3 class="subtitle-1 mt-5" align="center">KIV/PIA semester project</h3>
      <v-row>
        <v-col md="4" class="mx-auto">
          <v-card class="mx-auto mt-9">
            <v-tabs
              :centered="true"
              :grow="true">
              <v-tab>Login</v-tab>
              <v-tab>Register</v-tab>

              <v-tab-item class="pa-5" align="center">
                <span class="subtitle-1 font-weight-light">Already have an account? Sign in!</span>
                <v-text-field
                  v-model="email"
                  :rules="emailRules"
                  label="E-mail"
                  required
                ></v-text-field>

                <v-text-field
                  v-model="password"
                  label="Password"
                  :type="showPassword ? 'text' : 'password'"
                  :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  @click:append="showPassword = !showPassword"/>

                <v-btn text @click="loginUser">SIGN IN</v-btn>
              </v-tab-item>

              <v-tab-item class="pa-5" align="center">
                <span class="subtitle-1 font-weight-light">Do not have an account yet? Sign up, it's free!</span>
                <v-text-field
                  v-model="emailR"
                  :rules="emailRules"
                  label="E-mail"
                  required
                ></v-text-field>

                <v-text-field
                  v-model="username"
                  :rules="nameRules"
                  :counter="16"
                  label="Username"
                  required
                ></v-text-field>
                <v-text-field
                  v-model="passwordR"
                  label="Password"
                  :rules="passwordRules"
                  :counter="20"
                  :type="showPassword ? 'text' : 'password'"
                  :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  @click:append="showPassword = !showPassword"/>
                <v-text-field
                  label="Repeat password"
                  v-model="passwordRConfirm"
                  :rules="[passwordConfirmationRule]"
                  :type="showPassword ? 'text' : 'password'"
                />
                <v-btn text
                       @click="register">SIGN UP
                </v-btn>
              </v-tab-item>
            </v-tabs>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<script>
import Logo from '~/components/Logo.vue'
import VuetifyLogo from '~/components/VuetifyLogo.vue'

export default {
  computed: {
    passwordConfirmationRule() {
      return () => (this.passwordR === this.passwordRConfirm) || 'Password must match'
    }
  },

  data: () => ({

    showLogin: true,
    showPassword: false,
    valid: false,
    password: "",
    passwordR: "",
    passwordRConfirm: "",
    username: '',
    lastname: '',
    nameRules: [
      v => !!v || 'Username is required',
      v => v.length <= 16 || 'Username must be less than 16 characters',
    ],
    email: '',
    emailR: '',
    emailRules: [
      v => !!v || 'E-mail is required',
      v => /.+@.+/.test(v) || 'E-mail must be valid',
    ],
    passwordRules: [
      p => !!p || "Password is required",
      p => p.length >= 8 || "Password must consist of at least 8 characters",
      p => p.length <= 20 || "Password must consist of 20 characters at most",
      p => containsUppercase(p) || "Password must contain at least one uppercase character",
      p => containsLowercase(p) || "Password must contain at least one lowercase character",
      p => containsNumber(p) || "Password must contain at least one number",
    ],

  }),
  components: {
    Logo,
    VuetifyLogo
  },
  methods: {

    async loginUser() {
      await this.$store.dispatch("login/login", {
        email: this.email,
        password: this.password
      })
    },

    async register() {
      await this.$store.dispatch("login/register", {
        email: this.emailR,
        username: this.username,
        password: this.passwordR,
        login: true
      })
    }
  }
}

function containsNumber(password) {
  for (const c of password) {
    if (c >= '0' && c <= '9') {
      return true
    }
  }
  return false
}

function containsLowercase(password) {
  for (const c of password) {
    if (c.toLowerCase() === c) {
      return true
    }
  }
  return false
}

function containsUppercase(password) {
  for (const c of password) {
    if (c.toUpperCase() === c) {
      return true
    }
  }
  return false
}

</script>
