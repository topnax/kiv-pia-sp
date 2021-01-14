<template>
  <v-data-table
    :headers="headers"
    :items="users"
    :items-per-page="5"
    :loading="loading"
    loading-text="Loading... Please wait"
    class="elevation-1"
  >

    <template v-slot:top>
      <v-toolbar
        flat
      >
        <v-toolbar-title>Registered users list</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-dialog
          v-model="dialog"
          max-width="500px"
        >
          <v-card>
            <v-card-title>
              <span class="headline">Change {{ userEdit.username }}'s password</span>
            </v-card-title>

            <v-card-text>
              <v-container>
                <v-row>
                  <v-form v-model="changePasswordFormValid">
                    <v-text-field
                      v-model="newPassword"
                      type="password"
                      label="New password"
                      :rules="passwordRules"
                      required
                    ></v-text-field>
                  </v-form>
                </v-row>
              </v-container>
            </v-card-text>

            <v-card-actions>
              <v-spacer></v-spacer>
              <v-btn
                color="blue darken-1"
                text
                @click="close">
                Cancel
              </v-btn>
              <v-btn
                color="blue darken-1"
                text
                :disabled="!changePasswordFormValid"
                @click="save">
                Save
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>

    <template v-slot:item.admin="{ item }">
      <v-simple-checkbox
        v-model="item.admin"
        disabled
      ></v-simple-checkbox>
    </template>

    <template v-slot:item.actions="{ item }">

      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-icon
            @click="openChangePasswordDialog(item)"
            dark
            v-bind="attrs"
            v-on="on"
          >
            mdi-lock-reset
          </v-icon>
        </template>
        <span>Change user's password</span>
      </v-tooltip>

      <v-tooltip bottom v-if="!item.admin && item.username !== 'admin'">
        <template v-slot:activator="{ on, attrs }">
          <v-icon
            @click="promoteUser(item)"
            dark
            v-bind="attrs"
            v-on="on"
          >
            mdi-account-cowboy-hat
          </v-icon>
        </template>
        <span>Promote to admin</span>
      </v-tooltip>

      <v-tooltip bottom v-if="item.admin && item.username !== 'admin'">
        <template v-slot:activator="{ on, attrs }">
          <v-icon
            @click="demoteUser(item)"
            dark
            v-bind="attrs"
            v-on="on"
          >
            mdi-baby-carriage
          </v-icon>
        </template>
        <span>Demote from admin</span>
      </v-tooltip>
    </template>

  </v-data-table>
</template>

<script>
export default {
  name: "UserTable",
  props: {
    users: Array,
    loading: false,
  },

  methods: {
    demoteUser(user) {
      this.$store.dispatch("useradministration/changeUserRole", {userId: user.id, promote: false})
    },
    promoteUser(user) {
      this.$store.dispatch("useradministration/changeUserRole", {userId: user.id, promote: true})
    },
    openChangePasswordDialog(user) {
      this.userEdit = user
      this.dialog = true
    },
    close() {
      this.newPassword = ""
      this.dialog = false
    },
    async save() {
      this.dialog = false;
      await this.$store.dispatch("useradministration/changePassword", {
        userId: this.userEdit.id,
        password: this.newPassword
      })
      this.newPassword = ""
    },
  },
  data: () => ({
    dialog: false,
    changePasswordFormValid: false,
    userEdit: {
      username: String,
      id: Number
    },
    newPassword: "",
    passwordRules: [
      v => !!v || 'Password is required',
      v => v.length > 0 || 'Password must be longer than one character',
    ],
    headers: [
      {text: 'ID', value: 'id'},
      {text: 'Username', value: 'username'},
      {text: 'Email', value: 'email'},
      {text: 'Admin', value: 'admin'},
      {text: 'Actions', value: 'actions', sortable: false}
    ],
  })
}
</script>
